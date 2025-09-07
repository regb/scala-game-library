package main

import (
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"os/signal"
	"path/filepath"
	"strings"
	"syscall"
)

func main() {
	// Create temporary directory
	tempDir, err := os.MkdirTemp("", "scalajs-server-")
	if err != nil {
		log.Fatalf("Failed to create temp directory: %v", err)
	}

	// Setup cleanup
	cleanup := func() {
		fmt.Printf("Cleaning up temporary directory: %s\n", tempDir)
		os.RemoveAll(tempDir)
	}

	// Handle termination signals
	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt, syscall.SIGTERM)
	go func() {
		<-c
		cleanup()
		os.Exit(0)
	}()

	// Copy HTML file
	if htmlFile := os.Getenv("HTML_FILE"); htmlFile != "" {
		if err := copyFile(htmlFile, filepath.Join(tempDir, "index.html")); err != nil {
			log.Fatalf("Failed to copy HTML file: %v", err)
		}
	}

	// Copy JS file
	if jsFile := os.Getenv("JS_FILE"); jsFile != "" {
		// Extract target path from JS file path (remove bazel-out prefix)
		jsTargetPath := jsFile
		if idx := strings.Index(jsFile, "bazel-out/"); idx != -1 {
			// Find the next slash after bazel-out/[config]/bin/
			parts := strings.SplitN(jsFile[idx:], "/", 4)
			if len(parts) == 4 {
				jsTargetPath = parts[3]
			}
		}

		jsDir := filepath.Join(tempDir, filepath.Dir(jsTargetPath))
		if err := os.MkdirAll(jsDir, 0755); err != nil {
			log.Fatalf("Failed to create JS directory: %v", err)
		}

		if err := copyFile(jsFile, filepath.Join(tempDir, jsTargetPath)); err != nil {
			log.Fatalf("Failed to copy JS file: %v", err)
		}
	}

	// Copy static files
	if staticFiles := os.Getenv("STATIC_FILES"); staticFiles != "" {
		staticStripPrefix := os.Getenv("STATIC_STRIP_PREFIX")
		staticFolder := os.Getenv("STATIC_FOLDER")
		if staticFolder == "" {
			staticFolder = "static"
		}

		files := strings.Split(staticFiles, ";")
		for _, file := range files {
			if file == "" {
				continue
			}

			var targetPath string
			if staticStripPrefix != "" {
				// Strip prefix from file path
				if idx := strings.Index(file, staticStripPrefix); idx != -1 {
					targetPath = file[idx+len(staticStripPrefix):]
					targetPath = strings.TrimPrefix(targetPath, "/")
				} else {
					log.Fatalf("static_strip_prefix '%s' not found in file path: %s", staticStripPrefix, file)
				}
			} else {
				targetPath = filepath.Base(file)
			}

			staticDir := filepath.Join(tempDir, staticFolder, filepath.Dir(targetPath))
			if err := os.MkdirAll(staticDir, 0755); err != nil {
				log.Fatalf("Failed to create static directory: %v", err)
			}

			if err := copyFile(file, filepath.Join(tempDir, staticFolder, targetPath)); err != nil {
				log.Fatalf("Failed to copy static file: %v", err)
			}
		}
	}

	// Start server
	port := "8080"
	if p := os.Getenv("PORT"); p != "" {
		port = p
	}

	fileServer := http.FileServer(http.Dir(tempDir))
	http.Handle("/", fileServer)

	fmt.Printf("Server starting on port %s\n", port)
	fmt.Printf("Serving from directory: %s\n", tempDir)
	fmt.Printf("Visit http://localhost:%s\n", port)

	if err := http.ListenAndServe(":"+port, nil); err != nil {
		cleanup()
		log.Fatal(err)
	}
}

func copyFile(src, dst string) error {
	srcFile, err := os.Open(src)
	if err != nil {
		return err
	}
	defer srcFile.Close()

	dstFile, err := os.Create(dst)
	if err != nil {
		return err
	}
	defer dstFile.Close()

	_, err = io.Copy(dstFile, srcFile)
	return err
}
