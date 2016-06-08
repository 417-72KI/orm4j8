package jp.natsukishina.orm4j8;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jp.natsukishina.orm4j8.annotations.Column;
import jp.natsukishina.orm4j8.annotations.HasMany;
import jp.natsukishina.orm4j8.annotations.HasOne;
import jp.natsukishina.orm4j8.annotations.UseTable;
import jp.natsukishina.orm4j8.entity.BaseEntity;

@UseTable("test_data")
public class TestData extends BaseEntity {

	@Column(value = "id", primary = true)
	private Integer id;

	@Column("name")
	private String name;

	@Column("options")
	private String options;

	@Column("birthday")
	private LocalDate birthDay;

	@HasOne(targetClass = HasOneTestData.class, foreignKey = "test_data_id")
	private HasOneTestData hasOneTestData;

	@HasMany(targetClass = HasManyTestData.class, foreignKey = "test_data_id")
	private List<HasManyTestData> hasManyTestDataList;

	@Column("create_at")
	private LocalDateTime createDate;

	@Column("update_at")
	private LocalDateTime updateDate;

	TestData(int id, String name, String options, LocalDate birthDay) {
		this.id = id;
		this.name = name;
		this.options = options;
		this.birthDay = birthDay;
	}

	TestData(int id, String name, String options) {
		this.id = id;
		this.name = name;
		this.options = options;
	}

	TestData(String name, String options) {
		this.name = name;
		this.options = options;
	}

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

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public LocalDate getBirthDay() {
		return birthDay;
	}

	public void setBirthDay(LocalDate birthDay) {
		this.birthDay = birthDay;
	}

	public HasOneTestData getHasOneTestData() {
		return hasOneTestData;
	}

	public void setHasOneTestData(HasOneTestData hasOneTestData) {
		this.hasOneTestData = hasOneTestData;
	}

	public List<HasManyTestData> getHasManyTestDataList() {
		return hasManyTestDataList;
	}

	public void setHasManyTestDataList(List<HasManyTestData> hasManyTestDataList) {
		this.hasManyTestDataList = hasManyTestDataList;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

}
