package dev.gorban.zentuner.core

interface UseCase<in Params, out Result> {
    suspend fun execute(params: Params): Result
}
