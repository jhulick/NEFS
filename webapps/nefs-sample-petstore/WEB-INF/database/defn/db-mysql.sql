CREATE TABLE Lookup_Result_Type
(
    id INTEGER PRIMARY KEY, /* type.EnumerationIdColumn */
    caption VARCHAR(96) NOT NULL, /* type.TextColumn */
    abbrev VARCHAR(32) /* type.TextColumn */
);
CREATE  INDEX PK_Lookup_Result_Type on Lookup_Result_Type (id);

CREATE TABLE Record_Status
(
    id INTEGER PRIMARY KEY, /* type.EnumerationIdColumn */
    caption VARCHAR(96) NOT NULL, /* type.TextColumn */
    abbrev VARCHAR(32) /* type.TextColumn */
);
CREATE unique INDEX UNQ_RecStatus_abbrev on Record_Status (abbrev);
CREATE  INDEX PK_Record_Status on Record_Status (id);

CREATE TABLE Profile
(
    profile_id VARCHAR(32) PRIMARY KEY, /* type.TextColumn */
    account_id INTEGER, /* type.LongIntegerColumn */
    langpref INTEGER NOT NULL, /* type.EnumerationIdRefColumn */
    favcategory VARCHAR(32), /* type.TextColumn */
    mylstopt VARCHAR(32), /* type.TextColumn */
    banneropt VARCHAR(32) /* type.TextColumn */
);
CREATE  INDEX PK_Profile on Profile (profile_id);
CREATE  INDEX PR_profiles_account_id on Profile (account_id);

CREATE TABLE Account
(
    account_id INTEGER PRIMARY KEY, /* type.AutoIncColumn */
    login_id VARCHAR(32) NOT NULL, /* type.TextColumn */
    email VARCHAR(30) NOT NULL, /* type.TextColumn */
    firstname VARCHAR(20) NOT NULL, /* type.TextColumn */
    lastname VARCHAR(25) NOT NULL, /* type.TextColumn */
    status VARCHAR(32) NOT NULL, /* type.TextColumn */
    addr1 VARCHAR(64) NOT NULL, /* type.TextColumn */
    addr2 VARCHAR(64) NOT NULL, /* type.TextColumn */
    city VARCHAR(64) NOT NULL, /* type.TextColumn */
    state VARCHAR(64) NOT NULL, /* type.TextColumn */
    country VARCHAR(64) NOT NULL, /* type.TextColumn */
    phone VARCHAR(30) NOT NULL /* type.TextColumn */
);
CREATE  INDEX PK_Account on Account (account_id);

CREATE TABLE BannerData
(
    bannerdata_id VARCHAR(32) PRIMARY KEY, /* type.TextColumn */
    bannername VARCHAR(64) NOT NULL /* type.TextColumn */
);
CREATE  INDEX PK_BannerData on BannerData (bannerdata_id);

CREATE TABLE Category
(
    category_id VARCHAR(32) PRIMARY KEY, /* type.TextColumn */
    name VARCHAR(30) NOT NULL, /* type.TextColumn */
    descr VARCHAR(30) NOT NULL /* type.TextColumn */
);
CREATE  INDEX PK_Category on Category (category_id);

CREATE TABLE Product
(
    product_id VARCHAR(32) PRIMARY KEY, /* type.TextColumn */
    category_id VARCHAR(32), /* type.TextColumn */
    name VARCHAR(30) NOT NULL, /* type.TextColumn */
    descr VARCHAR(30) NOT NULL /* type.TextColumn */
);
CREATE  INDEX PK_Product on Product (product_id);
CREATE  INDEX PR_products_category_id on Product (category_id);

CREATE TABLE Item
(
    item_id VARCHAR(32) PRIMARY KEY, /* type.TextColumn */
    product_id VARCHAR(32), /* type.TextColumn */
    listprice FLOAT NOT NULL, /* type.CurrencyColumn */
    unitcost FLOAT, /* type.CurrencyColumn */
    supplier_id INTEGER, /* type.LongIntegerColumn */
    status VARCHAR(20) NOT NULL, /* type.TextColumn */
    name VARCHAR(30) NOT NULL, /* type.TextColumn */
    descr VARCHAR(30) NOT NULL, /* type.TextColumn */
    image VARCHAR(20) NOT NULL, /* type.TextColumn */
    attr1 VARCHAR(30), /* type.TextColumn */
    attr2 VARCHAR(30), /* type.TextColumn */
    attr3 VARCHAR(30) /* type.TextColumn */
);
CREATE  INDEX PK_Item on Item (item_id);
CREATE  INDEX PR_items_product_id on Item (product_id);

CREATE TABLE Supplier
(
    supplier_id INTEGER PRIMARY KEY, /* type.AutoIncColumn */
    name VARCHAR(64) NOT NULL, /* type.TextColumn */
    status VARCHAR(20) NOT NULL, /* type.TextColumn */
    addr1 VARCHAR(64) NOT NULL, /* type.TextColumn */
    addr2 VARCHAR(64) NOT NULL, /* type.TextColumn */
    city VARCHAR(64) NOT NULL, /* type.TextColumn */
    state VARCHAR(64) NOT NULL, /* type.TextColumn */
    country VARCHAR(64) NOT NULL, /* type.TextColumn */
    phone VARCHAR(30) NOT NULL /* type.TextColumn */
);
CREATE  INDEX PK_Supplier on Supplier (supplier_id);

CREATE TABLE Orders
(
    orders_id INTEGER PRIMARY KEY, /* type.AutoIncColumn */
    account_id INTEGER, /* type.LongIntegerColumn */
    orderdate DATE NOT NULL, /* type.DateColumn */
    shipaddr1 VARCHAR(64) NOT NULL, /* type.TextColumn */
    shipaddr2 VARCHAR(64) NOT NULL, /* type.TextColumn */
    shipcity VARCHAR(64) NOT NULL, /* type.TextColumn */
    shipstate VARCHAR(64) NOT NULL, /* type.TextColumn */
    shipcountry VARCHAR(64) NOT NULL, /* type.TextColumn */
    shipphone VARCHAR(30) NOT NULL, /* type.TextColumn */
    billaddr1 VARCHAR(64) NOT NULL, /* type.TextColumn */
    billaddr2 VARCHAR(64) NOT NULL, /* type.TextColumn */
    billcity VARCHAR(64) NOT NULL, /* type.TextColumn */
    billstate VARCHAR(64) NOT NULL, /* type.TextColumn */
    billcountry VARCHAR(64) NOT NULL, /* type.TextColumn */
    billphone VARCHAR(30) NOT NULL /* type.TextColumn */
);
CREATE  INDEX PK_Orders on Orders (orders_id);
CREATE  INDEX PR_ords_account_id on Orders (account_id);

CREATE TABLE OrderStatus
(
    orderstatus_id INTEGER PRIMARY KEY, /* type.AutoIncColumn */
    orders_id INTEGER, /* type.LongIntegerColumn */
    itemnum INTEGER NOT NULL, /* type.IntegerColumn */
    ts DATE NOT NULL, /* type.DateColumn */
    status VARCHAR(20) NOT NULL /* type.TextColumn */
);
CREATE  INDEX PK_OrderStatus on OrderStatus (orderstatus_id);
CREATE  INDEX PR_orderstat_orders_id on OrderStatus (orders_id);

CREATE TABLE LineItem
(
    lineitem_id INTEGER PRIMARY KEY, /* type.AutoIncColumn */
    orders_id INTEGER, /* type.LongIntegerColumn */
    itemnum INTEGER, /* type.IntegerColumn */
    item_id VARCHAR(32), /* type.TextColumn */
    quantity INTEGER NOT NULL, /* type.IntegerColumn */
    unitprice FLOAT NOT NULL /* type.CurrencyColumn */
);
CREATE  INDEX PK_LineItem on LineItem (lineitem_id);
CREATE  INDEX PR_lineitems_orders_id on LineItem (orders_id);
CREATE  INDEX PR_lineitems_item_id on LineItem (item_id);

CREATE TABLE Language
(
    id INTEGER PRIMARY KEY, /* type.EnumerationIdColumn */
    caption VARCHAR(96) NOT NULL, /* type.TextColumn */
    abbrev VARCHAR(32) /* type.TextColumn */
);
CREATE  INDEX PK_Language on Language (id);

insert into Lookup_Result_Type (id, caption, abbrev) values (0, 'ID', NULL);
insert into Lookup_Result_Type (id, caption, abbrev) values (1, 'Caption', NULL);
insert into Lookup_Result_Type (id, caption, abbrev) values (2, 'Abbreviation', NULL);

insert into Record_Status (id, caption, abbrev) values (0, 'Inactive', 'I');
insert into Record_Status (id, caption, abbrev) values (1, 'Active', 'A');
insert into Record_Status (id, caption, abbrev) values (99, 'Unknown', 'U');

insert into Language (id, caption, abbrev) values (0, 'English', NULL);
insert into Language (id, caption, abbrev) values (1, 'French', NULL);
insert into Language (id, caption, abbrev) values (2, 'Japanese', NULL);
insert into Language (id, caption, abbrev) values (3, 'Urdu', NULL);