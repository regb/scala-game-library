package main

import (
	"fmt"
	"log"
	"net/http"
	"os"
	"path/filepath"
)

func main() {
	port := "8080"
	if p := os.Getenv("PORT"); p != "" {
		port = p
	}

	// Get the current working directory (runfiles directory when run with bazel)
	wd, err := os.Getwd()
	if err != nil {
		log.Fatal(err)
	}

	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path == "/" || r.URL.Path == "/index.html" {
			http.ServeFile(w, r, filepath.Join(wd, "examples/snake/html5/index.html"))
			return
		}

		if r.URL.Path == "/index.js" {
			http.ServeFile(w, r, filepath.Join(wd, "examples/snake/index.js"))
			return
		}

		http.NotFound(w, r)
	})

	fmt.Printf("Server starting on port %s\n", port)
	fmt.Printf("Serving from directory: %s\n", wd)
	fmt.Printf("Visit http://localhost:%s to play the Snake game\n", port)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}
