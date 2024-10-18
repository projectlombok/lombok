
@jakarta.persistence.Entity(name = "Post") class Post {
  private @lombok.experimental.JpaAssociationSync @jakarta.persistence.OneToOne(mappedBy = "post") PostDetails details;

  Post() {
    super();
  }

  public @java.lang.SuppressWarnings("all") @lombok.Generated void updatePostDetails(PostDetails postDetails) {
    if ((postDetails == null))
        {
          if ((this.details != null))
              {
                this.details.setPost(null);
              }
        }
    else
        {
          postDetails.setPost(this);
        }
    this.details = postDetails;
  }
}

@jakarta.persistence.Entity(name = "PostDetails") class PostDetails {
  private @jakarta.persistence.OneToOne Post post;

  PostDetails() {
    super();
  }

  public void setPost(final Post post) {
    this.post = post;
  }
}
