package main

import (
	"bytes"
	"encoding/base64"
	"flag"
	"fmt"
	"http"
	"io"
	"log"
	"os"
	"path/filepath"
	"sync"
	"xml"
)

var programName string

func main() {
	programName = filepath.Base(os.Args[0])
	log.SetOutput(os.Stdout)

	err := realMain()
	if err != nil {
		log.Fatal(err)
	}
	os.Exit(0)
}

func realMain() (err os.Error) {
	repo := Repository{
		Client: http.DefaultClient,
	}

	flag.Usage = func() {
		fmt.Fprintf(os.Stderr, "usage: %s [options] CNXML [RESOURCE [...]]\n", programName)
		flag.PrintDefaults()
	}
	module := flag.String("module", "", "upload a new version of an existing module")
	flag.StringVar(&repo.URL, "url", "http://cnx-repo.appspot.com/atompub", "base URL for repository")
	flag.StringVar(&repo.ID, "repo-id", "cnx-repo", "repository ID")
	flag.Parse()

	if flag.NArg() == 0 {
		return fmt.Errorf("usage: %s takes 1 or more arguments", programName)
	}

	// Open CNXML file
	cnxmlFile, err := os.Open(flag.Arg(0))
	if err != nil {
		return err
	}
	defer cnxmlFile.Close()

	// Create module
	var editURL string
	if *module == "" {
		editURL, err = repo.CreateModule()
		if err != nil {
			return err
		}
		log.Printf("created %s", editURL)
	} else {
		entry, err := repo.VersionInfo(*module, "latest")
		if err != nil {
			return err
		}
		editURL = entry.URL("edit")
		log.Printf("using module %s, edit URL: %s", *module, editURL)
	}

	// Upload resources
	log.Print("starting resource upload")
	mapping := ResourceMapping{Version: "1"}
	resourceChan := make(chan Resource)
	wg := new(sync.WaitGroup)
	for _, arg := range flag.Args()[1:] {
		f, err := os.Open(arg)
		if err != nil {
			log.Printf("could not open %s: %v", arg, err)
			continue
		}
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
		}(filepath.Base(arg), f)
	}
	go func() {
		wg.Wait()
		close(resourceChan)
	}()

	for r := range resourceChan {
		mapping.Resource = append(mapping.Resource, r)
	}

	// Upload version
	log.Printf("uploading to %s", editURL)
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
