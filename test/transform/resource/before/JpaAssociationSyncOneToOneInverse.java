
@jakarta.persistence.Entity(name = "Post")
class Post {
    @lombok.experimental.JpaAssociationSync
    @lombok.experimental.JpaAssociationSync.Extra(inverseSideFieldName = "post")
    @jakarta.persistence.OneToOne
    private PostDetails details;
}

@jakarta.persistence.Entity(name = "PostDetails")
class PostDetails {
    @jakarta.persistence.OneToOne(mappedBy = "details")
    private Post post;

    public void setPost(final Post post) {
        this.post = post;
    }
}
