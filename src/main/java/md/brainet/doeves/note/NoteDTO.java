package md.brainet.doeves.note;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class NoteDTO {

        private String name;
        private String description;
        @JsonProperty("catalog_id")
        private Integer catalogId;

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public String getDescription() {
                return description;
        }

        public void setDescription(String description) {
                this.description = description;
        }

        public Integer getCatalogId() {
                return catalogId;
        }

        public void setCatalogId(Integer catalogId) {
                this.catalogId = catalogId;
        }
}
