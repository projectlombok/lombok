
@lombok.experimental.JpaAssociationSync
@jakarta.persistence.Entity(name = "Post")
class Post {
    @jakarta.persistence.OneToMany(mappedBy = "post")
    private java.util.List<PostComment> comments = new java.util.ArrayList<>();

    @jakarta.persistence.OneToOne(mappedBy = "post")
    private PostDetails details;

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
