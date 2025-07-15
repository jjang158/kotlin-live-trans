package com.livetrans.application

import android.content.Context
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 파일 다운로드
 * @param context Context
 * @param fileName 저장할 파일 이름
 * @param content 저장할 텍스트 내용
 * @return 저장된 파일 객체 (성공 시), 실패 시 null
 */
fun downloadTextFile(context: Context, fileName: String, content: String) {
    return try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val file = File(downloadsDir, fileName)
        val fos = FileOutputStream(file)
        fos.write(content.toByteArray())
        fos.close()

        Toast.makeText(context, "파일 저장 완료: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
    } catch (e: IOException) {
        Toast.makeText(context, "파일 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}