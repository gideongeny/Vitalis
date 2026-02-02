package com.gideongeng.Vitalis.TodoDatabase.Repository

import androidx.lifecycle.LiveData
import com.gideongeng.Vitalis.TodoDatabase.Todo
import com.gideongeng.Vitalis.TodoDatabase.TodoDao

class TodoRepositoryImp(val todoDao: TodoDao):TodoRepository{
    override suspend fun insertTodo(todo: Todo) {
        todoDao.insertTodo(todo)
    }

    override suspend fun deleteTodo(todo: Todo) {
        todoDao.deleteTodo(todo)
    }

    override suspend fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo)
    }

    override suspend fun getTodoById(id: Int): Todo? {
        return todoDao.getTodoById(id)
    }

    override fun getList(): LiveData<List<Todo>> {
        return  todoDao.getTodo()
    }
}

