package com.github.cgpablo.grpcmongo.blog.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final static Logger logger = LoggerFactory.getLogger(BlogServiceImpl.class);

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("mydb");
    private MongoCollection<Document> collection = database.getCollection("blog");

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {

        String METHOD_NAME = "createBlogService - ";
        logger.info("{} Received new request", METHOD_NAME);

        Blog blog = request.getBlog();

        Document document = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        logger.info("{} Persisting the blog...", METHOD_NAME);
        collection.insertOne(document);

        String id = document.getObjectId("_id").toString();
        logger.info("{} Persisted the blog with id {} successfully", METHOD_NAME, id);

        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder()
                        .setId(id)
                        .build())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {

        String METHOD_NAME = "readBlogService - ";
        logger.info("{} Received new request", METHOD_NAME);

        String blogId = request.getBlogId();
        logger.info("{} Searching for the blog with id {} ...", METHOD_NAME, blogId);
        Document result = null;

        try {
            result = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e) {
            responseObserver.onError(Status.NOT_FOUND
                    .augmentDescription(e.getLocalizedMessage())
                    .asRuntimeException());
        }

        if (result == null) {
            logger.info("{} Blog not found", METHOD_NAME);
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Blog not found")
                    .asRuntimeException());
        } else {
            logger.info("{} Blog found, sending response...", METHOD_NAME);
            Blog blog = documentToBlog(result);

            responseObserver.onNext(ReadBlogResponse.newBuilder()
                    .setBlog(blog)
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {

        String METHOD_NAME = "updateBlogService - ";
        logger.info("{} Received new request", METHOD_NAME);

        Blog blog = request.getBlog();

        String blogId = blog.getId();

        logger.info("{} Searching for the blog to update", METHOD_NAME);
        Document result = null;

        try {
            result = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e) {
            responseObserver.onError(Status.NOT_FOUND
                    .augmentDescription(e.getLocalizedMessage())
                    .asRuntimeException());
        }

        if (result == null) {
            logger.info("{} Blog not found", METHOD_NAME);
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Blog not found")
                    .asRuntimeException());
        } else {
            Document replacement = new Document("author_id", blog.getAuthorId())
                    .append("title", blog.getTitle())
                    .append("content", blog.getContent())
                    .append("_id", new ObjectId(blogId));

            logger.info("{} Updating the blog...", METHOD_NAME);
            collection.replaceOne(eq("_id", result.getObjectId("_id")), replacement);

            logger.info("{} Blog updated. Sending the response...", METHOD_NAME);
            responseObserver.onNext(UpdateBlogResponse.newBuilder()
                    .setBlog(documentToBlog(replacement))
                    .build());

            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {

        String METHOD_NAME = "deleteBlogService - ";
        logger.info("{} Received new request", METHOD_NAME);

        String blogId = request.getBlogId();
        DeleteResult result = null;
        try {
            result = collection.deleteOne(eq("_id", new ObjectId(blogId)));
        } catch (Exception e) {
            logger.info("{} Blog not found", METHOD_NAME);
            responseObserver.onError(Status.NOT_FOUND
                    .augmentDescription(e.getLocalizedMessage())
                    .asRuntimeException());
        }

        if (result.getDeletedCount() == 0) {
            logger.info("{} Blog not found", METHOD_NAME);
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Blog not found")
                    .asRuntimeException());
        } else {
            logger.info("{} Blog with id {} deleted", METHOD_NAME, blogId);
            responseObserver.onNext(DeleteBlogResponse.newBuilder()
                    .setBlogId(blogId)
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void listBlog(ListBlogRequest request, StreamObserver<ListBlogResponse> responseObserver) {

        String METHOD_NAME = "listBlogService - ";
        logger.info("{} Received new request", METHOD_NAME);

        collection.find().iterator().forEachRemaining(document -> responseObserver.onNext(
                ListBlogResponse.newBuilder().setBlog(documentToBlog(document)).build()
        ));

        responseObserver.onCompleted();
    }

    private Blog documentToBlog(Document document) {
        return Blog.newBuilder()
                .setAuthorId(document.getString("author_id"))
                .setTitle(document.getString("title"))
                .setContent(document.getString("content"))
                .setId(document.getObjectId("_id").toString())
                .build();
    }
}
