package shared.sample.primary.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_place_holder")
public class PlaceHolderEntity {
        @Id
        @Column(name = "ID", nullable = false)
        private long id;

        public long getId() {
                return id;
        }

        public void setId(long id) {
                this.id = id;
        }
}
