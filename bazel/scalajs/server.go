// Serves a static website directory (a scalajs_site) for local development.
//
// SITE_DIR names the directory, relative to the working directory, which is
// the runfiles tree under `bazel run`. PORT overrides the default port 8080.
package main

import (
	"fmt"
	"log"
	"net/http"
	"os"
	"path/filepath"
)

func main() {
	siteDir := os.Getenv("SITE_DIR")
	if siteDir == "" {
		log.Fatal("SITE_DIR is not set")
	}
	siteDir, err := filepath.Abs(siteDir)
	if err != nil {
		log.Fatalf("Cannot resolve the site directory: %v", err)
	}
	if info, err := os.Stat(siteDir); err != nil || !info.IsDir() {
		log.Fatalf("The site directory does not exist: %s", siteDir)
	}

	port := "8080"
	if p := os.Getenv("PORT"); p != "" {
		port = p
	}

	http.Handle("/", http.FileServer(http.Dir(siteDir)))

	fmt.Printf("Serving %s\n", siteDir)
	fmt.Printf("Visit http://localhost:%s\n", port)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}
