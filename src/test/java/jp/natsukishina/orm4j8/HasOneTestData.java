package jp.natsukishina.orm4j8;

import jp.natsukishina.orm4j8.annotations.Column;
import jp.natsukishina.orm4j8.annotations.HasOne;
import jp.natsukishina.orm4j8.annotations.UseTable;
import jp.natsukishina.orm4j8.entity.BaseEntity;

@UseTable("has_one_test_data")
public class HasOneTestData extends BaseEntity {
	@Column("id")
	private Integer id;

	@Column("name")
	private String name;

	@Column("test_data_id")
	private Integer testDataId;

	@HasOne(targetClass = HasOneHasOneTestData.class, foreignKey = "has_one_test_data_id")
	private HasOneHasOneTestData hasOneHasOneTestData;

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

	public HasOneHasOneTestData getHasOneHasOneTestData() {
		return hasOneHasOneTestData;
	}

	public void setHasOneHasOneTestData(HasOneHasOneTestData hasOneHasOneTestData) {
		this.hasOneHasOneTestData = hasOneHasOneTestData;
	}

	@UseTable("has_one_has_one_test_data")
	public static class HasOneHasOneTestData extends BaseEntity {
		@Column("id")
		private Integer id;

		@Column("name")
		private String name;

		@Column("has_one_test_data_id")
		private Integer hasOneTestDataId;

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

		public Integer getHasOneTestDataId() {
			return hasOneTestDataId;
		}

		public void setHasOneTestDataId(Integer hasOneTestDataId) {
			this.hasOneTestDataId = hasOneTestDataId;
		}

	}
}
