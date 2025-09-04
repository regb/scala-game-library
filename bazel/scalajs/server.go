package main

import (
	"fmt"
	"log"
	"net/http"
	"os"
)

func main() {
	if len(os.Args) < 2 {
		log.Fatal("Usage: server <directory-to-serve>")
	}

	serveDir := os.Args[1]

	port := "8080"
	if p := os.Getenv("PORT"); p != "" {
		port = p
	}

	if _, err := os.Stat(serveDir); os.IsNotExist(err) {
		log.Fatalf("Directory does not exist: %s", serveDir)
	}

	fileServer := http.FileServer(http.Dir(serveDir))

	http.Handle("/", fileServer)

	fmt.Printf("Server starting on port %s\n", port)
	fmt.Printf("Serving from directory: %s\n", serveDir)
	fmt.Printf("Visit http://localhost:%s\n", port)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}
