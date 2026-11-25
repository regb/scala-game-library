// Puts Chrome and everything it loads in one directory that runs on any x86-64
// Linux, whatever is installed on it.
//
// The arguments are the output directory, then `kind=path` for each file, where
// the kind is chrome, library, font or license.
package main

import (
	"bytes"
	"fmt"
	"io"
	"log"
	"os"
	"path/filepath"
	"strings"
)

const (
	executable = "chrome-headless-shell"
	loader     = "ld-linux-x86-64.so.2"
)

func main() {
	arguments := os.Args[1:]
	if len(arguments) == 1 && strings.HasPrefix(arguments[0], "@") {
		content, err := os.ReadFile(arguments[0][1:])
		check(err)
		arguments = strings.Split(strings.TrimSpace(string(content)), "\n")
	}
	output := arguments[0]
	for _, argument := range arguments[1:] {
		kind, path, _ := strings.Cut(argument, "=")
		name := filepath.Base(path)
		switch kind {
		case "chrome":
			// The path below the directory of the same name in the repository.
			_, relative, found := strings.Cut(path, "/chrome/")
			if !found {
				log.Fatalf("Not in a chrome directory: %s", path)
			}
			copyFile(path, filepath.Join(output, relative))
		case "library":
			copyFile(path, filepath.Join(output, "lib", name))
		case "font":
			copyFile(path, filepath.Join(output, "fonts", name))
		case "license":
			copyFile(path, filepath.Join(output, "licenses", filepath.Base(filepath.Dir(path))))
		default:
			log.Fatalf("Unknown kind: %s", argument)
		}
	}
	check(os.Rename(filepath.Join(output, "lib", loader), filepath.Join(output, loader)))
	relocate(filepath.Join(output, executable))
}

// The kernel starts an executable with the loader it names, by an absolute path
// of the system. This makes it the one in the working directory instead. The new
// path is shorter and takes the place of the old.
func relocate(path string) {
	content, err := os.ReadFile(path)
	check(err)
	from := []byte("/lib64/" + loader + "\x00")
	if count := bytes.Count(content, from); count != 1 {
		log.Fatalf("Expected the loader once in %s, found it %d times", path, count)
	}
	to := append([]byte("./"+loader), make([]byte, len(from)-len("./"+loader))...)
	check(os.WriteFile(path, bytes.Replace(content, from, to, 1), 0o755))
}

func copyFile(from, to string) {
	info, err := os.Stat(from)
	check(err)
	check(os.MkdirAll(filepath.Dir(to), 0o755))
	source, err := os.Open(from)
	check(err)
	defer source.Close()
	mode := os.FileMode(0o644)
	if info.Mode()&0o100 != 0 {
		mode = 0o755
	}
	target, err := os.OpenFile(to, os.O_CREATE|os.O_WRONLY|os.O_TRUNC, mode)
	check(err)
	_, err = io.Copy(target, source)
	check(err)
	check(target.Close())
}

func check(err error) {
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}
