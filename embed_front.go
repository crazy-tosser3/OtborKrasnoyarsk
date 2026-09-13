package main

import (
	"embed"
	"io/fs"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

//go:embed all:frontend/.output/public
var frontendFS embed.FS

func SetupFrontend(router *gin.Engine) {
	distFS, err := fs.Sub(frontendFS, "frontend/.output/public")
	if err != nil {
		panic(err)
	}

	fileServer := http.FileServer(http.FS(distFS))

	router.NoRoute(func(c *gin.Context) {
		path := strings.TrimPrefix(c.Request.URL.Path, "/")

		if strings.HasPrefix(path, "api/") ||
			strings.HasPrefix(path, "swagger/") {
			c.JSON(http.StatusNotFound, gin.H{
				"error": "not found",
			})
			return
		}

		if _, err := distFS.Open(path); err == nil {
			fileServer.ServeHTTP(c.Writer, c.Request)
			return
		}

		c.Request.URL.Path = "/"
		fileServer.ServeHTTP(c.Writer, c.Request)
	})
}
