
@jakarta.persistence.Entity(name = "Post")
class Post {
    @lombok.experimental.JpaAssociationSync
    @lombok.experimental.JpaAssociationSync.Extra(inverseSideFieldName = "posts")
    @jakarta.persistence.ManyToMany
    private java.util.Set<Tag> tags = new java.util.HashSet<>();
}

@jakarta.persistence.Entity(name = "Tag")
class Tag {
    @jakarta.persistence.ManyToMany(mappedBy = "tags")
    private java.util.Set<Post> posts = new java.util.HashSet<>();

    public java.util.Set<Post> getPosts() {
        return this.posts;
    }
}
