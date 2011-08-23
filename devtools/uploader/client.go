package main

import (
	"bytes"
	"encoding/base64"
	"flag"
	"fmt"
	"http"
	"io"
	"os"
	"path/filepath"
	"xml"
)

var (
	repositoryURL string
	repositoryID  string
)

func main() {
	err := realMain()
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
	os.Exit(0)
}

const cnxmlName = "index_auto_generated.cnxml"

func realMain() (err os.Error) {
	module := flag.String("module", "", "upload a new version of an existing module")
	flag.StringVar(&repositoryURL, "url", "http://100.cnx-repo.appspot.com/atompub", "base URL for repository")
	flag.StringVar(&repositoryID, "repo-id", "cnx-repo", "repository ID")
	flag.Parse()

	if flag.NArg() != 1 {
		return os.NewError("client takes 1 argument")
	}

	// Upload resources
	mapping := ResourceMapping{Version: 1}
	directory, err := os.Open(flag.Arg(0))
	if err != nil {
		return err
	}
	defer directory.Close()

	fi, err := directory.Readdir(0)
	if err != nil {
		return err
	}
	for _, info := range fi {
		if info.IsRegular() && info.Name != cnxmlName && info.Name[0] != '.' {
			fmt.Println(info.Name)
			f, err := os.Open(filepath.Join(flag.Arg(0), info.Name))
			if err == nil {
				entry, err := uploadToBlobstore(info.Name, f)
				f.Close()
				if err == nil {
					mapping.Resource = append(mapping.Resource, Resource{
						Name:         info.Name,
						RepositoryID: repositoryID,
						ResourceID:   entry.ID,
					})
				} else {
					fmt.Printf("error uploading %s: %v\n", info.Name, err)
				}
			} else {
				fmt.Printf("error uploading %s: %v\n", info.Name, err)
			}
		}
	}

	// Create module
	var editURL string
	if *module == "" {
		editURL, err = createModule()
		if err != nil {
			return err
		}
		fmt.Println("Created", editURL)
	} else {
		entry, err := fetchVersionInfo(*module, "latest")
		if err != nil {
			return err
		}
		editURL = entry.URL("edit")
	}

	// Upload version
	fmt.Println("Uploading to", editURL)
	cnxmlFile, err := os.Open(filepath.Join(flag.Arg(0), cnxmlName))
	if err != nil {
		return err
	}
	defer cnxmlFile.Close()
	return uploadVersion(editURL, cnxmlFile, mapping)
}

func showModuleInfo(module string) os.Error {
	result, err := fetchVersionInfo(module, "latest")
	if err != nil {
		return err
	}
	fmt.Printf("id=%s\n", result.ID)
	for _, link := range result.Link {
		fmt.Printf("link rel=%s href=%s\n", link.Rel, link.Href)
	}

	var c Container
	var b bytes.Buffer
	io.Copy(&b, newDecoder(bytes.NewBufferString(result.Content.Content)))
	b.WriteString("e>") // TODO(light): get rid of this
	err = xml.Unmarshal(&b, &c)
	if err != nil {
		return err
	}
	io.Copy(os.Stdout, newDecoder(bytes.NewBuffer(c.CNXMLDoc.Data)))
	io.Copy(os.Stdout, newDecoder(bytes.NewBuffer(c.ResourceMappingDoc.Data)))

	return nil
}

func fetchVersionInfo(module, version string) (*AtomEntry, os.Error) {
	var client http.Client
	r, err := client.Get(repositoryURL + "/module/" + module + "/" + version)
	if err != nil {
		return nil, err
	}
	var entry AtomEntry
	err = xml.Unmarshal(r.Body, &entry)
	if err != nil {
		return nil, err
	}
	return &entry, err
}

func createModule() (string, os.Error) {
	entry, err := post(new(http.Client), repositoryURL+"/module/", PublishAtomEntry{
		XMLName: xml.Name{atomNamespace, "entry"},
	})
	if err != nil {
		return "", err
	}
	return entry.URL("edit"), err
}

func uploadVersion(url string, cnxml io.Reader, resourceMapping ResourceMapping) os.Error {
	container, err := NewContainer(cnxml, marshalReader(resourceMapping))
	io.Copy(os.Stdout, marshalReader(resourceMapping))
	if err != nil {
		return err
	}
	_, err = update(new(http.Client), url, PublishAtomEntry{
		XMLName: xml.Name{atomNamespace, "entry"},
		Content: &Content{
			Type:    "text",
			Content: container.Encode(),
		},
	})
	return err
}

func marshalReader(val interface{}) io.Reader {
	pr, pw := io.Pipe()
	go func() {
		err := xml.Marshal(pw, val)
		if err != nil {
			pw.CloseWithError(err)
		}
		pw.Close()
	}()
	return pr
}

type Container struct {
	XMLName  xml.Name `xml:"resource-entry-value"`
	CNXMLDoc struct {
		XMLName xml.Name `xml:"cnxml-doc"`
		Data    []byte   `xml:"chardata"`
	}
	ResourceMappingDoc struct {
		XMLName xml.Name `xml:"resource-mapping-doc"`
		Data    []byte   `xml:"chardata"`
	}
}

func NewContainer(cnxml, mapping io.Reader) (*Container, os.Error) {
	encodedCNXMLDoc, err := encodeReader(cnxml)
	if err != nil {
		return nil, err
	}
	encodedMapping, err := encodeReader(mapping)
	if err != nil {
		return nil, err
	}
	c := new(Container)
	c.CNXMLDoc.Data = encodedCNXMLDoc
	c.ResourceMappingDoc.Data = encodedMapping
	return c, nil
}

func (c *Container) Encode() string {
	pr, pw := io.Pipe()
	go func() {
		xml.Marshal(pw, c)
		pw.Close()
	}()
	data, _ := encodeReader(pr)
	return string(data)
}

type encoder struct {
	e1, e2 io.WriteCloser
}

func newEncoder(w io.Writer) io.WriteCloser {
	e := encoder{e1: base64.NewEncoder(base64.URLEncoding, w)}
	e.e2 = base64.NewEncoder(base64.URLEncoding, e.e1)
	return e
}

func (enc encoder) Write(p []byte) (n int, err os.Error) {
	return enc.e2.Write(p)
}

func (enc encoder) Close() os.Error {
	if err := enc.e2.Close(); err != nil {
		return err
	}
	if err := enc.e1.Close(); err != nil {
		return err
	}
	return nil
}

func encodeReader(r io.Reader) ([]byte, os.Error) {
	var b bytes.Buffer
	e := newEncoder(&b)
	_, err := io.Copy(e, r)
	if err != nil {
		return nil, err
	}
	e.Close()
	return b.Bytes(), nil
}

func newDecoder(r io.Reader) io.Reader {
	return base64.NewDecoder(base64.URLEncoding, base64.NewDecoder(base64.URLEncoding, r))
}
