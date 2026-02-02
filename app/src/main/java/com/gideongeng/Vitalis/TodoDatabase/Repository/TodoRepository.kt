package com.gideongeng.Vitalis.TodoDatabase.Repository

import androidx.lifecycle.LiveData
import com.gideongeng.Vitalis.TodoDatabase.Todo
interface TodoRepository{
    suspend fun insertTodo(todo: Todo)
    suspend fun deleteTodo(todo:Todo)
    suspend fun updateTodo(todo:Todo)
    suspend fun getTodoById(id:Int):Todo?
    fun  getList(): LiveData<List<Todo>>
}
