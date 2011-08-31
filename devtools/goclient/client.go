package main

import (
	"bytes"
	"encoding/base64"
	"flag"
	"http"
	"io"
	"log"
	"os"
	"path/filepath"
	"sync"
	"xml"
)

func main() {
	err := realMain()
	if err != nil {
		log.Fatal(err)
	}
	os.Exit(0)
}

const cnxmlName = "index_auto_generated.cnxml"

func realMain() (err os.Error) {
	repo := Repository{
		Client: http.DefaultClient,
	}

	module := flag.String("module", "", "upload a new version of an existing module")
	flag.StringVar(&repo.URL, "url", "http://cnx-repo.appspot.com/atompub", "base URL for repository")
	flag.StringVar(&repo.ID, "repo-id", "cnx-repo", "repository ID")
	flag.Parse()

	if flag.NArg() != 1 {
		return os.NewError("client takes 1 argument")
	}

	// Upload resources
	mapping := ResourceMapping{Version: "1"}
	directory, err := os.Open(flag.Arg(0))
	if err != nil {
		return err
	}
	defer directory.Close()

	fi, err := directory.Readdir(0)
	if err != nil {
		return err
	}

	resourceChan := make(chan Resource)
	wg := new(sync.WaitGroup)
	for _, info := range fi {
		if info.IsRegular() && info.Name != cnxmlName && info.Name[0] != '.' {
			if f, err := os.Open(filepath.Join(flag.Arg(0), info.Name)); err == nil {
				wg.Add(1)
				go func(name string, f *os.File) {
					defer f.Close()
					defer wg.Done()
					r, err := uploadFile(&repo, name, f)
					if err != nil {
						log.Printf("error uploading %s: %v", name, err)
						return
					}
					log.Printf("uploaded %s", name)
					resourceChan <- r
				}(info.Name, f)
			}
		}
	}
	go func() {
		wg.Wait()
		close(resourceChan)
	}()

	for r := range resourceChan {
		mapping.Resource = append(mapping.Resource, r)
	}

	// Create module
	var editURL string
	if *module == "" {
		editURL, err = repo.CreateModule()
		if err != nil {
			return err
		}
		log.Printf("Created %s", editURL)
	} else {
		entry, err := repo.VersionInfo(*module, "latest")
		if err != nil {
			return err
		}
		editURL = entry.URL("edit")
	}

	// Upload version
	log.Printf("Uploading to %s", editURL)
	cnxmlFile, err := os.Open(filepath.Join(flag.Arg(0), cnxmlName))
	if err != nil {
		return err
	}
	defer cnxmlFile.Close()
	return repo.UploadVersion(editURL, cnxmlFile, mapping)
}

func uploadFile(repo *Repository, name string, f *os.File) (Resource, os.Error) {
	entry, err := repo.UploadResource(name, f)
	if err != nil {
		return Resource{}, err
	}
	return Resource{
		Name: name,
		LocationInformation: LocationInfo{
			Repository: RepositoryInfo{
				RepositoryID: repo.ID,
				ResourceID:   entry.ID,
			},
		},
	}, nil
}

func pipe(f func(io.Writer) os.Error) io.Reader {
	pr, pw := io.Pipe()
	go func() {
		err := f(pw)
		if err != nil {
			pw.CloseWithError(err)
			return
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

func encodeReader(r io.Reader) ([]byte, os.Error) {
	var b bytes.Buffer
	e := base64.NewEncoder(base64.StdEncoding, &b)
	_, err := io.Copy(e, r)
	if err != nil {
		return nil, err
	}
	e.Close()
	return b.Bytes(), nil
}
