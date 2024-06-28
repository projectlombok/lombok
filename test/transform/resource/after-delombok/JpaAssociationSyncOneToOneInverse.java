
@jakarta.persistence.Entity(name = "Post")
class Post {
    @jakarta.persistence.OneToOne
    private PostDetails details;

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public void updatePostDetails(PostDetails postDetails) {
        if (postDetails == null) {
            if (this.details != null) {
                this.details.setPost(null);
            }
        } else {
            postDetails.setPost(this);
        }

        this.details = postDetails;
    }
}

@jakarta.persistence.Entity(name = "PostDetails")
class PostDetails {
    @jakarta.persistence.OneToOne(mappedBy = "details")
    private Post post;

    public void setPost(final Post post) {
        this.post = post;
    }
}
