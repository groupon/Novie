Novie - Querying
================

## Querying mechanism

### Filters

Novie offers a powerful querying mechanism. It is designed to allow the user to make any query filtered on all the dimensions and informations available of each end-point. 
Its general format is :
`dimension.information=OperatorValue`

The information is not mandatory. If it is not specified, the system will use the default filter for the specified dimension (This filter may or may not be an information). The dimension and information name are case insensitive.

The constraint is composed of an operator and a value. If the operator is missing the default operator is equal. The format of the value depends of the type of the information filtered (see table below). 

The different operators are 

| Operator   	| Sense 		     | Example                                                                                       																					|
|:-------------:|:--------------------------:| -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| value		| equal			     | `dimension.info=10` <br/> the information “info” of the dimension “dimension” is equal to 10.																					|
| `!`value	| not equal 		     | `dimension.info=!10` <br/> the information “info” of the dimension “dimension” is not equal to 10. 																				|
| `]`value	| greater than 	             | `dimension.info=]10` <br/> the information “info” of the dimension “dimension” is greater than 10. 																				|
| `[`value	| greater than or equal to   | `dimension.info=[10` <br/> the information “info” of the dimension “dimension” is greater than 10 or equal to 10. 																|
| value`[`	| lower than 	             | `dimension.info=10[` <br/> the information “info” of the dimension “dimension” is lower than 10.																					|
| value`]`	| lower than or equal to     | `dimension.info=10]` <br/> the information “info” of the dimension “dimension” is lower than 10 or equal to 10.																	|
| val`*`ue	| like (only for String)     | `dimension.info=make*` <br/> the information “info” of the dimension “dimension” start by make (eq is like make%.) <br/> Note the * sign can be use multiples times.				|
| `!`val`*`ue	| not like (only for String) | `dimension.info=!make*` <br/> the information “info” of the dimension “dimension” not sart with make (eq is not like make*.) <br/> Note the * sign can be use multiples times.	|

### Combinations

In order to build complex queries, this mechanism allows the user to combine different constraints. The two combinaison are AND, represented by putting multiple times the same information’s parameter in the query and the OR for an information which is represented by the following specific format in the value of the query parameter : 
`dimension.info=OperatorValue,OperatorValue,OperatorValue....`

Examples:
- `dimension.info=]10&dimension.info=20[` means dimension.info is greater than 10 and is lower than 20 
- `dimension.info=10,20,[30` means dimension.info equals 10 or equals 20 or is greater or equals to 30 

### Grouping

The usage of the grouping is mandatory. The grouping is specified by using the keyword group as key in the query parameter. Eg: `group=dimension.info`

The api allows the user to make grouping on every available dimension or information. It allows also to group on multiple levels. To that purpose, use | as separator of grouping information. Eg: `group=dimension.info|dimension2.info`

_Please Note_: Because of the design of a Star schema, It’s not possible to group on several information of the same dimension.

The information name is not mandatory. If it is not specified, the grouping will be make on the “default grouping key” of the dimension and the result will be displayed with the “default grouping displayed information” information of the dimension. Otherwise, both the grouping and the result will use the specified dimension.

For example for the dimension user with the user.id as “default grouping key” and user.name as “default grouping displayed information”. 
- Let’s say there are 3 users in the data which are - name(id): 
	- John (1)
	- Joe (2)
	- John (3)
- If the group is set to “user” the resulting groups will be:
	- John
	- Joe
	- John
- If the group is set to “user.name” the resulting groups will be:
	- John
	- Joe

### Sorting

The api allows the user to sort the results on every available dimension, information or measure. 
- `sort=dimension.info`
- `sort=measure_1`

It allows also to sorting on multiple levels. To that purpose, use | as separator of grouping information. Eg 
- `sort=dimension.info|dimension2.info`
- `sort=measure_1|dimension2.info`

The usage of the character `-` before a sorting criteria, will reverse the order of the result for this criteria. Eg `sort=dimension.info|-dimension2.info will sort on dimension.info in natural order followed by dimension2.info in the reverse order.`

## Other Functionalities

### Timezone
The api allow the user to switch between different timezones. This is designed to use an alternate dimension depending on the timezone. For instance to aggregate in the proper timezone date-time data.

Use the keyword `timezone` in order to activate this functionality. `Eg timezone=CST`

If the timezone is not specified, the api will not use the timezone switching.

_Attention_: The timezone switching functionality must be activated in the endPoint definition.

Some documentation about timezone abreviation & code:
- General [www.timeanddate.com/library/abbreviations/timezones/](http://www.timeanddate.com/library/abbreviations/timezones/)
- North America [www.timeanddate.com/library/abbreviations/timezones/na/](http://www.timeanddate.com/library/abbreviations/timezones/na/)
- Europe [www.timeanddate.com/library/abbreviations/timezones/eu/](http://www.timeanddate.com/library/abbreviations/timezones/eu/)


## Pagination
The api allow the user to paginate the resulting records. The pagination is specified  by using the two keywords `pageSize` and `page`, both have to be present.
- `pageSize` is the number of record returned for page;
- `page` is the number of page starting from 1;

Example:
`page=2&pageSize=10`



## Supported data type & format

### Supported data type

The supported data format are

| Type name | Description            | Text format                              | SQL Type equivalent |
| --------- | ---------------------- | ---------------------------------------- | ------------------- |
| DATE      | Date type without time | yyyyMMddZ eg *20130701-0500*             | Date                |
| DATETIME  | Date type with time    | yyyyMMddTHH:mmZ eg *20130701T01:30+0100* | Timestamp           |
| DECIMAL   | Decimal number         | #.# eg *1000.23*                         | Decimal             |
| INTEGER   | Integer number         | # eg *1000*                              | Integer             |
| STRING    | String                 | eg *The document*                        | Varchar             |

### Supported response format
Novie supports 3 type of response format. JSON, XML and CSV. There is two way to select the response format:
- Calling the endpoint  with the proper “Accept” Header. Eg: application/json
- Calling the endpoint  with the proper suffix. Eg: endPoint.json

It should be noted that if no suffit is used, the format will depends on the HTTP header `ACCEPT`.

| Response Type    | `Accept` Header  | `endPoint` suffix |
|:----------------:|:----------------:|:-----------------:|
| application/json | application/json | .json             |
| application/xml  | application/xml <br/> text/xml | .xml |
| text/csv         | text/csv         | .csv              |

In case of text/csv, the Content-Disposition of the response is attachment and the filename is “records.csv”
