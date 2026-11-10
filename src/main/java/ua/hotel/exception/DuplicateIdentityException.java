package ua.hotel.exception;

public class DuplicateIdentityException extends IllegalStateException {

    private final String repositoryName;
    private final String identity;

    public DuplicateIdentityException(String repositoryName, String identity) {
        super(String.format("Duplicate identity '%s' in repository '%s'", identity, repositoryName));
        this.repositoryName = repositoryName;
        this.identity = identity;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getIdentity() {
        return identity;
    }
}
