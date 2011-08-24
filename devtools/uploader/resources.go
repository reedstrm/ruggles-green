package main

import (
	"http"
	"io"
	"mime"
	"os"
	"path"
	"xml"
)

type ResourceMapping struct {
	XMLName  xml.Name `xml:"resources"`
	Version  string   `xml:"attr"`
	Resource []Resource
}

type Resource struct {
	XMLName      xml.Name `xml:"resource"`
	Name         string   `xml:"attr"`
	RepositoryID string   `xml:"location-information>repository>repository-id"`
	ResourceID   string   `xml:"location-information>repository>resource-id"`
}

func uploadToBlobstore(name string, r io.Reader) (*AtomEntry, os.Error) {
	var client http.Client
	entry, err := post(&client, repositoryURL+"/resource", PublishAtomEntry{
		Title: name,
	})
	if err != nil {
		return nil, err
	}

	resp, err := client.Post(entry.URL("blobstore"), mime.TypeByExtension(path.Ext(name)), r)
	// TODO(light): check response
	if err != nil {
		return nil, err
	}
	resp.Body.Close()

	return entry, nil
}
