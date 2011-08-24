package main

import (
	"http"
	"os"
	"xml"
)

type PublishAtomEntry struct {
	XMLName xml.Name `xml:"http://www.w3.org/2005/Atom entry"`
	Title   string   `xml:"title"`
	Content *Content
}

type AtomEntry struct {
	ID   string `xml:"id"`
	Link []Link
	PublishAtomEntry
}

func (entry AtomEntry) URL(rel string) string {
	for _, link := range entry.Link {
		if link.Rel == rel {
			return link.Href
		}
	}
	return ""
}

type Content struct {
	XMLName xml.Name `xml:"http://www.w3.org/2005/Atom content"`
	Type    string   `xml:"attr"`
	Content string   `xml:"chardata"`
}

type Link struct {
	XMLName xml.Name `xml:"http://www.w3.org/2005/Atom link"`
	Rel     string   `xml:"attr"`
	Href    string   `xml:"attr"`
}

const atomEntryType = `application/atom+xml;type=entry;charset="utf-8"`

func post(client *http.Client, url string, entry interface{}) (*AtomEntry, os.Error) {
	resp, err := client.Post(url, atomEntryType, marshalReader(entry))
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	// TODO(light): Check response code
	var fullEntry AtomEntry
	if err := xml.Unmarshal(resp.Body, &fullEntry); err != nil {
		return nil, err
	}
	return &fullEntry, nil
}

func update(client *http.Client, url string, entry interface{}) (*AtomEntry, os.Error) {
	req, err := http.NewRequest("PUT", url, marshalReader(entry))
	if err != nil {
		return nil, err
	}
	req.Header.Set("Content-Type", atomEntryType)
	resp, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	// TODO(light): Check response code
	var fullEntry AtomEntry
	if err := xml.Unmarshal(resp.Body, &fullEntry); err != nil {
		return nil, err
	}
	return &fullEntry, nil
}
