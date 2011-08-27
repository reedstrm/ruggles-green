package main

import (
	"fmt"
	"http"
	"io"
	"os"
	"xml"
)

type AtomError struct {
	URL        string
	Op         string
	StatusCode int
}

func (err AtomError) String() string {
	return fmt.Sprintf("%s %s: server returned status code %d", err.Op, err.URL, err.StatusCode)
}

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

func create(client *http.Client, url string, entry interface{}) (*AtomEntry, os.Error) {
	resp, err := client.Post(url, atomEntryType, pipe(func(w io.Writer) os.Error {
		w.Write([]byte(xml.Header))
		return xml.Marshal(w, entry)
	}))
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusCreated && resp.StatusCode != http.StatusOK {
		return nil, AtomError{
			URL:        url,
			Op:         "create",
			StatusCode: resp.StatusCode,
		}
	}

	var fullEntry AtomEntry
	if err := xml.Unmarshal(resp.Body, &fullEntry); err != nil {
		return nil, err
	}
	return &fullEntry, nil
}

func update(client *http.Client, url string, entry interface{}) (*AtomEntry, os.Error) {
	req, err := http.NewRequest("PUT", url, pipe(func(w io.Writer) os.Error {
		w.Write([]byte(xml.Header))
		return xml.Marshal(w, entry)
	}))
	if err != nil {
		return nil, err
	}
	req.Header.Set("Content-Type", atomEntryType)

	resp, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusCreated && resp.StatusCode != http.StatusOK {
		return nil, AtomError{
			URL:        url,
			Op:         "update",
			StatusCode: resp.StatusCode,
		}
	}

	var fullEntry AtomEntry
	if err := xml.Unmarshal(resp.Body, &fullEntry); err != nil {
		return nil, err
	}
	return &fullEntry, nil
}
