FROM golang:latest

WORKDIR /app

COPY ./go.mod ./go.sum ./

RUN go mod download

COPY .env .env

COPY . .

RUN go build -o main .

CMD ["./main"]