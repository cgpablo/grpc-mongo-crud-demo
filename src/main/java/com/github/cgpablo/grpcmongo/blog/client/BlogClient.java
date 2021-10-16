package com.github.cgpablo.grpcmongo.blog.client;

import com.github.cgpablo.grpcmongo.blog.service.BlogServiceImpl;
import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlogClient {

    private final static Logger logger = LoggerFactory.getLogger(BlogClient.class);

    public static void main(String[] args) {
        BlogClient client = new BlogClient();
        client.start();
    }

    private void start() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        //createBlog(channel);
        //readBlog(channel);
        //updateBlog(channel);
        //deleteBlog(channel);
        listBlog(channel);
    }

    private void listBlog(ManagedChannel channel) {
        String METHOD_NAME = "deleteBlogClient - ";

        BlogServiceGrpc.BlogServiceBlockingStub blogServiceBlockingStub = BlogServiceGrpc.newBlockingStub(channel);

        blogServiceBlockingStub.listBlog(ListBlogRequest.newBuilder().build()).forEachRemaining(
                listBlogResponse -> logger.info("{} Received response: {}", METHOD_NAME, listBlogResponse.getBlog())
        );
    }

    private void deleteBlog(ManagedChannel channel) {
        String METHOD_NAME = "deleteBlogClient - ";

        BlogServiceGrpc.BlogServiceBlockingStub blogServiceBlockingStub = BlogServiceGrpc.newBlockingStub(channel);

        DeleteBlogResponse response = blogServiceBlockingStub.deleteBlog(DeleteBlogRequest.newBuilder()
                .setBlogId("616b25a1c9c3ce63cac6723c")
                .build());

        logger.info("{} Received response: {}", METHOD_NAME, response.toString());
    }

    private void updateBlog(ManagedChannel channel) {
        String METHOD_NAME = "updateBlogClient - ";

        BlogServiceGrpc.BlogServiceBlockingStub blogServiceBlockingStub = BlogServiceGrpc.newBlockingStub(channel);

        Blog blog = Blog.newBuilder()
                .setId("616b016c7592d32ab7869d30")
                .setAuthorId("Author2")
                .setTitle("Updated blog")
                .setContent("Updated content")
                .build();

        UpdateBlogResponse response = blogServiceBlockingStub.updateBlog(UpdateBlogRequest.newBuilder()
                .setBlog(blog)
                .build());

        logger.info("{} Received response: {}", METHOD_NAME, response.toString());
    }

    private void readBlog(ManagedChannel channel) {
        String METHOD_NAME = "readBlogClient - ";

        BlogServiceGrpc.BlogServiceBlockingStub blogServiceBlockingStub = BlogServiceGrpc.newBlockingStub(channel);

        ReadBlogResponse response = blogServiceBlockingStub.readBlog(ReadBlogRequest.newBuilder()
                .setBlogId("616b284e5425196e28e5fc84")
                .build());

        logger.info("{} Received response: {}", METHOD_NAME, response.toString());
    }

    private void createBlog(ManagedChannel channel) {
        String METHOD_NAME = "createBlogClient - ";

        BlogServiceGrpc.BlogServiceBlockingStub blogServiceBlockingStub = BlogServiceGrpc.newBlockingStub(channel);

        Blog blog = Blog.newBuilder()
                .setAuthorId("Author")
                .setTitle("New blog")
                .setContent("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut vulputate elit ac metus dignissim, iaculis iaculis lacus tincidunt. Aenean vitae ligula ut arcu pulvinar rutrum. Etiam porttitor euismod faucibus.")
                .build();

        CreateBlogResponse response = blogServiceBlockingStub.createBlog(CreateBlogRequest.newBuilder()
                .setBlog(blog)
                .build());

        logger.info("{} Received response: {}", METHOD_NAME, response.toString());
    }

}
