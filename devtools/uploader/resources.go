package main

import (
	"xml"
)

type ResourceMapping struct {
	XMLName  xml.Name `xml:"resources"`
	Version  string   `xml:"attr"`
	Resource []Resource
}

type Resource struct {
	XMLName             xml.Name `xml:"resource"`
	Name                string   `xml:"attr"`
	LocationInformation LocationInfo

	// TODO(light): When my patch to xml goes through.
	//RepositoryID string   `xml:"location-information>repository>repository-id"`
	//ResourceID   string   `xml:"location-information>repository>resource-id"`
}

type LocationInfo struct {
	XMLName    xml.Name `xml:"location-information"`
	Repository RepositoryInfo
}

type RepositoryInfo struct {
	XMLName      xml.Name `xml:"repository"`
	RepositoryID string   `xml:"repository-id"`
	ResourceID   string   `xml:"resource-id"`
}
