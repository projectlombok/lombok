
@jakarta.persistence.Entity(name = "Post")
class Post {
    @jakarta.persistence.OneToMany(mappedBy = "post")
    private java.util.List<PostComment> comments = new java.util.ArrayList<>();

    @jakarta.persistence.OneToOne(mappedBy = "post")
    private PostDetails details;

    @jakarta.persistence.ManyToMany(mappedBy = "posts")
    private java.util.Set<Tag> tags = new java.util.HashSet<>();

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public void addAnotherName(PostComment anotherName) {
        this.comments.add(anotherName);
        anotherName.setPost(this);
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public void removeAnotherName(PostComment anotherName) {
        this.comments.remove(anotherName);
        anotherName.setPost(null);
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public void updateAnotherName1(PostDetails anotherName1) {
        if (anotherName1 == null) {
            if (this.details != null) {
                this.details.setPost(null);
            }
        } else {
            anotherName1.setPost(this);
        }

        this.details = anotherName1;
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public void addAnotherName2(Tag anotherName2) {
        this.tags.add(anotherName2);
        anotherName2.getPosts().add(this);
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public void removeAnotherName2(Tag anotherName2) {
        this.tags.remove(anotherName2);
        anotherName2.getPosts().remove(this);
    }
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
