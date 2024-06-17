
@jakarta.persistence.Entity(name = "Post") class Post {
  private @lombok.experimental.JpaAssociationSync @jakarta.persistence.OneToMany(mappedBy = "post") java.util.List<PostComment> comments = new java.util.ArrayList<>();

  Post() {
    super();
  }

  public @java.lang.SuppressWarnings("all") @lombok.Generated void addPostComment(PostComment postComment) {
    this.comments.add(postComment);
    postComment.setPost(this);
  }

  public @java.lang.SuppressWarnings("all") @lombok.Generated void removePostComment(PostComment postComment) {
    this.comments.remove(postComment);
    postComment.setPost(null);
  }
}

@jakarta.persistence.Entity(name = "PostComment") class PostComment {
  private @jakarta.persistence.ManyToOne Post post;

  PostComment() {
    super();
  }

  public void setPost(final Post post) {
    this.post = post;
  }
}
