
@lombok.experimental.JpaAssociationSync @jakarta.persistence.Entity(name = "Post") class Post {
  private @lombok.experimental.JpaAssociationSync.Extra(paramName = "anotherName") @jakarta.persistence.OneToMany(mappedBy = "post") java.util.List<PostComment> comments = new java.util.ArrayList<>();
  private @lombok.experimental.JpaAssociationSync.Extra(paramName = "anotherName1") @jakarta.persistence.OneToOne(mappedBy = "post") PostDetails details;
  private @lombok.experimental.JpaAssociationSync.Extra(paramName = "anotherName2") @jakarta.persistence.ManyToMany(mappedBy = "posts") java.util.Set<Tag> tags = new java.util.HashSet<>();

  Post() {
    super();
  }

  public @java.lang.SuppressWarnings("all") @lombok.Generated void addAnotherName(PostComment anotherName) {
    this.comments.add(anotherName);
    anotherName.setPost(this);
  }

  public @java.lang.SuppressWarnings("all") @lombok.Generated void removeAnotherName(PostComment anotherName) {
    this.comments.remove(anotherName);
    anotherName.setPost(null);
  }

  public @java.lang.SuppressWarnings("all") @lombok.Generated void updateAnotherName1(PostDetails anotherName1) {
    if ((anotherName1 == null))
        {
          if ((this.details != null))
              {
                this.details.setPost(null);
              }
        }
    else
        {
          anotherName1.setPost(this);
        }
    this.details = anotherName1;
  }

  public @java.lang.SuppressWarnings("all") @lombok.Generated void addAnotherName2(Tag anotherName2) {
    this.tags.add(anotherName2);
    anotherName2.getPosts().add(this);
  }

  public @java.lang.SuppressWarnings("all") @lombok.Generated void removeAnotherName2(Tag anotherName2) {
    this.tags.remove(anotherName2);
    anotherName2.getPosts().remove(this);
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
