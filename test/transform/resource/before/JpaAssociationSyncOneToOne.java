
@jakarta.persistence.Entity(name = "Post")
class Post {
    @lombok.experimental.JpaAssociationSync
    @jakarta.persistence.OneToOne(mappedBy = "post")
    private PostDetails details;
}

@jakarta.persistence.Entity(name = "PostDetails")
class PostDetails {
    @jakarta.persistence.OneToOne
    private Post post;

    public void setPost(final Post post) {
        this.post = post;
    }
}
