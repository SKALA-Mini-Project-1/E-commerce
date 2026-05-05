#!/bin/bash

docker run --rm -it --name my-app --network bridge -p 8888:80 skala-posts-get:1.0
