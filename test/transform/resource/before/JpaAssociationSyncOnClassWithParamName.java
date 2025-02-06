
@lombok.experimental.JpaAssociationSync
@jakarta.persistence.Entity(name = "Post")
class Post {
    @lombok.experimental.JpaAssociationSync.Extra(paramName = "anotherName")
    @jakarta.persistence.OneToMany(mappedBy = "post")
    private java.util.List<PostComment> comments = new java.util.ArrayList<>();

    @lombok.experimental.JpaAssociationSync.Extra(paramName = "anotherName1")
    @jakarta.persistence.OneToOne(mappedBy = "post")
    private PostDetails details;

    @lombok.experimental.JpaAssociationSync.Extra(paramName = "anotherName2")
    @jakarta.persistence.ManyToMany(mappedBy = "posts")
    private java.util.Set<Tag> tags = new java.util.HashSet<>();
}

@jakarta.persistence.Entity(name = "PostComment")
class PostComment {
    @jakarta.persistence.ManyToOne
    private Post post;

    public void setPost(final Post post) {
        this.post = post;
    }
}

@jakarta.persistence.Entity(name = "PostDetails")
class PostDetails {
    @jakarta.persistence.OneToOne
    private Post post;

    public void setPost(final Post post) {
        this.post = post;
    }
}

@jakarta.persistence.Entity(name = "Tag")
class Tag {
    @jakarta.persistence.ManyToMany
    private java.util.Set<Post> posts = new java.util.HashSet<>();

    public java.util.Set<Post> getPosts() {
        return this.posts;
    }
}
