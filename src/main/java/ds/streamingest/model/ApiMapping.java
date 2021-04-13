package ds.streamingest.model;

public class ApiMapping {
    private String resource;
    private PartitionKeyExtractorTypes location;
    private String specification;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public PartitionKeyExtractorTypes getLocation() {
        return location;
    }

    public void setLocation(PartitionKeyExtractorTypes location) {
        this.location = location;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    @Override
    public String toString() {
        return "ApiMapping{" +
                "resource='" + resource + '\'' +
                ", location='" + location + '\'' +
                ", specification='" + specification + '\'' +
                '}';
    }
}
