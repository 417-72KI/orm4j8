package jp.natsukishina.orm4j8;

import static jp.natsukishina.orm4j8.Setup4DBTest.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DBTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		config();
		DB.beginTransaction();
		dropTables();
		createTables();
		DB.commit();
		System.out.println();
	}

	@Before
	public void setUp() {
		DB.beginTransaction();
		insertTestRecords();
		DB.commit();
		System.out.println();
	}

	@After
	public void tearDown() {
		DB.beginTransaction();
		deleteTestRecords();
		DB.commit();
		System.out.println();
	}

	@AfterClass
	public static void tearDownAfterClass() {
		DB.beginTransaction();
		dropTables();
		DB.commit();
	}

	// Select

	@Test
	public void find_正常() {
		TestData data = DB.find(TestData.class).where("name", "=", "name2").execute().get();
		assertThat(data.getId(), is(2));
		assertThat(data.getName(), is("name2"));
		assertThat(data.getBirthDay(), is(LocalDate.of(1990, 6, 1)));
		assertThat(data.getCreateDate().toLocalDate(), is(DBUtil.toLocalDate(new Date())));
		assertThat(data.getUpdateDate().toLocalDate(), is(DBUtil.toLocalDate(new Date())));
	}

	@Test
	public void find_部分一致_正常() {
		TestData data = DB.find(TestData.class).where("name", "like", "name%").execute().get();
		assertThat(data.getId(), is(1));
		assertThat(data.getName(), is("name1"));
	}

	@Test
	public void find_正常_存在しない() {
		TestData data = DB.find(TestData.class).where("name", "=", "not exist user").execute().get();
		assertThat(data, nullValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void find_String_null() {
		DB.find(null).execute();
	}

	@Test
	public void findList_部分一致_正常() {
		List<TestData> dataList = DB.find(TestData.class).where("name", "like", "name%").execute().getAll();
		assertThat(dataList.size(), is(insertRow));
		dataList.forEach(u -> assertThat(u.getName(), startsWith("name")));
	}

	@Test
	public void findList_正常() {
		List<TestData> dataList = DB.find(TestData.class).where("options", "=", "hoge").execute().getAll();
		dataList.forEach(u -> assertThat(u.getOptions(), is("hoge")));
	}

	@Test
	public void findList_null_正常() {
		List<TestData> dataList = DB.find(TestData.class).where("options", "=", null).execute().getAll();
		dataList.forEach(u -> assertThat(u.getOptions(), nullValue()));
	}

	@Test
	public void findList_hasOne_正常() {
		List<TestData> dataList = DB.find(TestData.class).where("options", "=", "hoge").execute().getAll();
		assertThat(dataList.size(), is(not(0)));
		dataList.forEach(u -> {
			assertThat(u.getOptions(), is("hoge"));
			HasOneTestData hotd = u.getHasOneTestData();
			assertThat(hotd, notNullValue());
			assertThat(hotd.getTestDataId(), is(u.getId()));
			assertThat(hotd.getName(), is("name" + u.getId()));
		});
	}

	@Test
	public void findList_loadHasMany_正常() {
		List<TestData> dataList = DB.find(TestData.class).execute().getAll();
		assertThat(dataList.size(), is(not(0)));
		dataList.forEach(d -> {
			d.loadHasMany(HasManyTestData.class);
			assertThat(d.getHasManyTestDataList(), notNullValue());
		});
		dataList.stream().flatMap(d -> d.getHasManyTestDataList().stream()).forEach(hmtd -> {
			assertThat(hmtd.getOptions(), is("fuga"));
		});

	}

	@Test
	public void findList_limit3_正常() {
		List<TestData> dataList = DB.find(TestData.class).where("options", "=", "hoge").limit(3).execute().getAll();
		dataList.forEach(u -> assertThat(u.getOptions(), is("hoge")));
		assertThat(dataList.size(), is(3));
	}

	@Test
	public void findList_offset2_limit2_正常() {
		List<TestData> dataList = DB.find(TestData.class).where("options", "=", "hoge").orderBy("id").offset(2).limit(2)
				.execute().getAll();
		assertThat(dataList.size(), is(2));
		dataList.forEach(u -> assertThat(u.getOptions(), is("hoge")));
		assertThat(dataList.get(0).getId(), is(3));
		assertThat(dataList.get(0).getName(), is("name3"));
		assertThat(dataList.get(1).getId(), is(4));
		assertThat(dataList.get(1).getName(), is("name4"));
	}

	@Test
	public void findList_正常_存在しない() {
		List<TestData> dataList = DB.find(TestData.class).where("name", "=", "not exist user").execute().getAll();
		assertThat(dataList.isEmpty(), is(true));
	}

	// Insert

	@Test
	public void save_Insert_primaryKeyあり_正常系() {
		TestData data = new TestData(6, "new_name", "new_option");
		assertThat(DB.save(data), is(true));
		data = DB.find(data.getClass()).where("id", "=", "6").execute().get();
		assertThat(data, notNullValue());
		assertThat(data.getId(), is(6));
	}

	@Test
	public void save_Insert_primaryKey無し_正常系() {
		int newId = insertRow + 1;
		TestData data = new TestData("new_name", "new_option");
		assertThat(DB.save(data), is(true));
		data = DB.find(data.getClass()).where("id", "=", newId).execute().get();
		assertThat(data, notNullValue());
		assertThat(data.getId(), is(newId));
		assertThat(data.getName(), is("new_name"));
		assertThat(data.getOptions(), is("new_option"));

		newId++;
		data = new TestData("new_name", "new_option");
		assertThat(DB.save(data), is(true));
		data = DB.find(data.getClass()).where("id", "=", newId).execute().get();
		assertThat(data, notNullValue());
		assertThat(data.getId(), is(newId));
		assertThat(data.getName(), is("new_name"));
		assertThat(data.getOptions(), is("new_option"));
	}

	// Update

	@Test
	public void save_Update_正常系() {
		TestData data = DB.find(TestData.class).where("id", "=", 1).execute().get();
		assertThat(data, notNullValue());
		data.setName("new_name");
		data.setOptions("fuga");
		data.setUpdateDate(DBUtil.toLocalDateTime(new Date()));
		assertThat(DB.save(data), is(true));
		data = DB.find(TestData.class).where("id", "=", 1).execute().get();
		assertThat(data, notNullValue());
		assertThat(data.getId(), is(1));
		assertThat(data.getName(), is("new_name"));
		assertThat(data.getOptions(), is("fuga"));
		assertThat(data.getUpdateDate().toLocalDate(), is(DBUtil.toLocalDate(new Date())));
	}

	@Test
	public void save_Update_setnull_正常系() {
		TestData data = DB.find(TestData.class).where("id", "=", 1).execute().get();
		assertThat(data, notNullValue());
		data.setName("new_name");
		data.setOptions(null);
		assertThat(DB.save(data), is(true));
		data = DB.find(TestData.class).where("id", "=", 1).execute().get();
		assertThat(data, notNullValue());
		assertThat(data.getId(), is(1));
		assertThat(data.getName(), is("new_name"));
		assertThat(data.getOptions(), nullValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void save_null() {
		DB.save(null);
	}

	// Delete

	@Test
	public void delete_正常系() {
		List<TestData> dataList = DB.find(TestData.class).where("name", "like", "name%").execute().getAll();
		assertThat(dataList.size(), is(insertRow));
		assertThat(DB.delete(new TestData(1, "name1", "hoge")), is(true));
		dataList = DB.find(TestData.class).where("name", "like", "name%").execute().getAll();
		assertThat(dataList.size(), is(insertRow - 1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void delete_null() {
		DB.delete(null);
	}

}
