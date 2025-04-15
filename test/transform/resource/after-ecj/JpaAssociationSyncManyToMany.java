
@jakarta.persistence.Entity(name = "Post") class Post {
  private @lombok.experimental.JpaAssociationSync @jakarta.persistence.ManyToMany(mappedBy = "posts") java.util.Set<Tag> tags = new java.util.HashSet<>();

  Post() {
    super();
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

@jakarta.persistence.Entity(name = "Tag") class Tag {
  private @jakarta.persistence.ManyToMany java.util.Set<Post> posts = new java.util.HashSet<>();

  Tag() {
    super();
  }

  public java.util.Set<Post> getPosts() {
    return this.posts;
  }
}
