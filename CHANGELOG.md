# Changelog

-------------------------------------------------------------------------------------------------------------
## 1.0.6(2024-11.25)
* 使用NativeQuery可以执行简单的insert、update.

## 1.0.5(2024-11.20)

* 修复param bindable bug.
* change default DeciderParam.bindable to true

## 1.0.4(2024-11-20)

* Add @DeciderParam标注，明确定义是否参与表名决策, DeciderParam.bindable指示是否同时做为查询参数
* 查询参数中可以直接使用TableNameDecider的实例，优先级高于@SharedQuery标注指定的
* 支持查询返回Primitive and wrapper type,此功能不完善


## 1.0.3(2024-11-12)

* @EnableSharedRepositories支持Repeatable
* 用来决策表名的查询参数可以不使用@Param标注,不参与查询条件, 在决策器中使用相对应的参数序号字符串(0-based)获取
* 从JPA扩展，支持事务，以后会支持实体的操作

## 1.0.2(2024-10-29)

* 如果没有匹配的表，返回空集合

## 1.0.1(2024-10-25)

* 支持TableNameDecider的实现类使用@Autowire注入beans

### 特性

* 实现在同一数据库内执行水平分表查询，当前仅支持NativeQuery
* 查询结果支持Projection和DataClass
