package jp.natsukishina.orm4j8;

import jp.natsukishina.orm4j8.annotations.Column;
import jp.natsukishina.orm4j8.annotations.UseTable;
import jp.natsukishina.orm4j8.entity.BaseEntity;

@UseTable("has_many_test_data")
public class HasManyTestData extends BaseEntity {
	@Column("id")
	private Integer id;

	@Column("name")
	private String name;

	@Column("test_data_id")
	private Integer testDataId;

	@Column("options")
	private String options;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getTestDataId() {
		return testDataId;
	}

	public void setTestDataId(Integer testDataId) {
		this.testDataId = testDataId;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

}
