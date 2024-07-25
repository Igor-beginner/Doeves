package md.brainet.doeves.exception;

import java.util.List;

public class CatalogsNotExistException extends RuntimeException {

    private final List<Integer> catalogsId;


    public CatalogsNotExistException(List<Integer> catalogsId) {
        super("Request was reject cause the next catalog id's are not exists: "
                .concat(catalogsId.toString()));
        this.catalogsId = catalogsId;
    }


    public List<Integer> getCatalogsId() {
        return catalogsId;
    }
}
