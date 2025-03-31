
@jakarta.persistence.Entity(name = "Post")
class Post {
    @jakarta.persistence.ManyToMany
    private java.util.Set<Tag> tags = new java.util.HashSet<>();

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getPosts().add(this);
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getPosts().remove(this);
    }
}

@jakarta.persistence.Entity(name = "Tag")
class Tag {
    @jakarta.persistence.ManyToMany(mappedBy = "tags")
    private java.util.Set<Post> posts = new java.util.HashSet<>();

    public java.util.Set<Post> getPosts() {
        return this.posts;
    }
}
