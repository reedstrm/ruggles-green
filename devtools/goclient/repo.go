package main

import (
	"fmt"
	"http"
	"io"
	"mime/multipart"
	"os"
	"xml"
)

type Repository struct {
	*http.Client
	ID  string
	URL string
}

func (repo *Repository) VersionInfo(module, version string) (*AtomEntry, os.Error) {
	r, err := repo.Get(repo.URL + "/module/" + module + "/" + version)
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

func (repo *Repository) CreateModule() (string, os.Error) {
	entry, err := create(repo.Client, repo.URL+"/module/", PublishAtomEntry{})
	if err != nil {
		return "", err
	}
	return entry.URL("edit"), err
}

func (repo *Repository) UploadVersion(url string, cnxml io.Reader, resourceMapping ResourceMapping) os.Error {
	container, err := NewContainer(cnxml, pipe(func(w io.Writer) os.Error {
		w.Write([]byte(xml.Header))
		return xml.Marshal(w, resourceMapping)
	}))
	if err != nil {
		return err
	}
	_, err = update(repo.Client, url, PublishAtomEntry{
		Content: &Content{
			Type:    "text",
			Content: container.Encode(),
		},
	})
	return err
}

func (repo *Repository) UploadResource(name string, r io.Reader) (*AtomEntry, os.Error) {
	entry, err := create(repo.Client, repo.URL+"/resource", PublishAtomEntry{
		Title: name,
	})
	if err != nil {
		return nil, err
	}

	typeChan := make(chan string)
	body := pipe(func(w io.Writer) os.Error {
		m := multipart.NewWriter(w)
		defer m.Close()
		typeChan <- m.FormDataContentType()
		f, err := m.CreateFormFile("file", name)
		if err != nil {
			return err
		}
		_, err = io.Copy(f, r)
		return err
	})

	resp, err := repo.Post(entry.URL("blobstore"), <-typeChan, body)
	if err != nil {
		return nil, err
	}
	resp.Body.Close()
	if resp.StatusCode != http.StatusFound {
		return nil, BlobstoreError{Name: name, StatusCode: resp.StatusCode}
	}

	return entry, nil
}

type BlobstoreError struct {
	Name       string
	StatusCode int
}

func (err BlobstoreError) String() string {
	return fmt.Sprintf("blobstore: got status code %d for %s", err.StatusCode, err.Name)
}
