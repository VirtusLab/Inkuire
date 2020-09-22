package org.virtuslab.inkuire.intellij.plugin.actions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.ui.components.JBList
import com.intellij.ui.table.JBTable
import org.apache.http.client.utils.URIBuilder
import org.jdesktop.swingx.prompt.PromptSupport
import org.jetbrains.annotations.NotNull
import org.virtuslab.inkuire.model.OutputFormat
import java.awt.Color
import java.awt.Dimension
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import javax.swing.*
import javax.swing.table.DefaultTableModel


class QueryAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val client = HttpClient.newHttpClient()

        val input = JTextField().apply {
            preferredSize = Dimension(500, 30)
            PromptSupport.setPrompt("List<String>.() -> Int", this)
        }
        val resultArea = JBTable().apply {
            border = BorderFactory.createLineBorder(Color.GRAY)
            preferredSize = Dimension(1000, 500)
        }

        val gson = Gson()

        val searchListener = {
            val txt = input.text
            val uriBuilder = URIBuilder("http://127.0.0.1:8080/forSignature")
            uriBuilder.setParameter("signature", txt)

            val request: HttpRequest = HttpRequest
                    .newBuilder(uriBuilder.build())
                    .header("Content-Type", "application/json")
                    .GET()
                    .build()

            val response: HttpResponse<String> = client.send(request, BodyHandlers.ofString())

            val parsed = gson.fromJson(response.body(), OutputFormat::class.java)

            val (data,headers) = parseOutputToModel(parsed)

            resultArea.model = DefaultTableModel(headers, data)
            resultArea.setShowColumns(true)
        }

        val searchButton = JButton("Search").apply {
            addActionListener { searchListener() }
        }


        val dialogPanel = JPanel().apply {
            preferredSize = Dimension(1050, 600)
            add(JScrollPane(resultArea).apply {
                preferredSize = Dimension(1025,550)
            })
            add(searchButton)
            add(input)
        }

        val db = DialogBuilder().apply {
            setOkOperation(searchListener)
            setCenterPanel(dialogPanel)
        }

        db.show()
    }

    private fun parseOutputToModel(output: OutputFormat): Pair<Array<String>, Array<Array<String>>> {
        val columnNames = arrayOf("Name", "Signature", "Localization")
        val data = output.matches.map{
            arrayOf(it.functionName, it.prettifiedSignature, it.localization)
        }.toTypedArray()
        return Pair(columnNames, data)
    }

    private fun parseStringArray(str: String): Array<String> {
        val size = str.length
        return str.take(size - 1).takeLast(size - 2).split(", ").map {
            val s = it.length
            it.take(s - 1).takeLast(s - 2)
        }.toTypedArray()
    }
}