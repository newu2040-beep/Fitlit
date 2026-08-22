package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ActivityLogEntity
import com.example.data.local.entity.FridgeItemEntity
import com.example.data.local.entity.LoggedFoodEntity
import com.example.data.local.entity.MealPlanEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.WaterLogEntity
import com.example.data.local.entity.WeightLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FitlitDao {

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    // Meal Plans
    @Query("SELECT * FROM meal_plans ORDER BY dayOfWeek ASC, id ASC")
    fun getAllMealPlans(): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE dayOfWeek = :dayOfWeek ORDER BY id ASC")
    fun getMealPlansForDay(dayOfWeek: Int): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE id = :id LIMIT 1")
    suspend fun getMealPlanById(id: Long): MealPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(meal: MealPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlans(meals: List<MealPlanEntity>)

    @Update
    suspend fun updateMealPlan(meal: MealPlanEntity)

    @Query("UPDATE meal_plans SET isEaten = :isEaten WHERE id = :id")
    suspend fun setMealEaten(id: Long, isEaten: Boolean)

    @Query("UPDATE meal_plans SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setMealFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM meal_plans")
    suspend fun clearMealPlans()

    @Query("DELETE FROM meal_plans WHERE id = :id")
    suspend fun deleteMealPlanById(id: Long)

    // Food Logs
    @Query("SELECT * FROM logged_food WHERE dateStr = :dateStr ORDER BY timestamp DESC")
    fun getLoggedFoodForDate(dateStr: String): Flow<List<LoggedFoodEntity>>

    @Query("SELECT * FROM logged_food ORDER BY timestamp DESC")
    fun getAllLoggedFood(): Flow<List<LoggedFoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoggedFood(food: LoggedFoodEntity): Long

    @Delete
    suspend fun deleteLoggedFood(food: LoggedFoodEntity)

    // Fridge Items
    @Query("SELECT * FROM fridge_items ORDER BY category ASC, addedTimestamp DESC")
    fun getAllFridgeItems(): Flow<List<FridgeItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFridgeItem(item: FridgeItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFridgeItems(items: List<FridgeItemEntity>)

    @Update
    suspend fun updateFridgeItem(item: FridgeItemEntity)

    @Query("DELETE FROM fridge_items WHERE id = :id")
    suspend fun deleteFridgeItem(id: Long)

    @Query("DELETE FROM fridge_items")
    suspend fun clearFridgeItems()

    // Weight Logs
    @Query("SELECT * FROM weight_logs ORDER BY timestamp ASC")
    fun getAllWeightLogs(): Flow<List<WeightLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weightLog: WeightLogEntity): Long

    // Activity Logs
    @Query("SELECT * FROM activity_logs WHERE dateStr = :dateStr ORDER BY timestamp DESC")
    fun getActivityLogsForDate(dateStr: String): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllActivityLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(activity: ActivityLogEntity): Long

    // Water Logs
    @Query("SELECT * FROM water_logs WHERE dateStr = :dateStr")
    fun getWaterLogsForDate(dateStr: String): Flow<List<WaterLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(waterLog: WaterLogEntity): Long
}
