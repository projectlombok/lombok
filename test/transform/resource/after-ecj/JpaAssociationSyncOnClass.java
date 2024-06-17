
@lombok.experimental.JpaAssociationSync @jakarta.persistence.Entity(name = "Post") class Post {
  private @jakarta.persistence.OneToMany(mappedBy = "post") java.util.List<PostComment> comments = new java.util.ArrayList<>();
  private @jakarta.persistence.OneToOne(mappedBy = "post") PostDetails details;
  private @jakarta.persistence.ManyToMany(mappedBy = "posts") java.util.Set<Tag> tags = new java.util.HashSet<>();

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

  public @java.lang.SuppressWarnings("all") @lombok.Generated void addTag(Tag tag) {
    this.tags.add(tag);
    tag.getPosts().add(this);
  }

  public @java.lang.SuppressWarnings("all") @lombok.Generated void removeTag(Tag tag) {
    this.tags.remove(tag);
    tag.getPosts().remove(this);
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

@jakarta.persistence.Entity(name = "PostDetails") class PostDetails {
  private @jakarta.persistence.OneToOne Post post;

  PostDetails() {
    super();
  }

  public void setPost(final Post post) {
    this.post = post;
  }
}

@jakarta.persistence.Entity(name = "Tag") class Tag {
  private @jakarta.persistence.ManyToMany java.util.Set<Post> posts = new java.util.HashSet<>();

  Tag() {
    super();
  }

  public java.util.Set<Post> getPosts() {
    return this.posts;
  }
}
