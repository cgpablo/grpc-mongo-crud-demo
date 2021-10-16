package com.github.cgpablo.grpcmongo.blog.server;

import com.github.cgpablo.grpcmongo.blog.service.BlogServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class BlogServer {

    private static final int PORT = 50051;
    private Server server;

    public static void main(String[] args)
            throws InterruptedException, IOException {
        BlogServer server = new BlogServer();
        server.start();
        server.blockUntilShutdown();
    }

    private void start() throws IOException {
        server = ServerBuilder.forPort(PORT)
                .addService(new BlogServiceImpl())
                .build()
                .start();
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server == null) {
            return;
        }
        server.awaitTermination();
    }

}
