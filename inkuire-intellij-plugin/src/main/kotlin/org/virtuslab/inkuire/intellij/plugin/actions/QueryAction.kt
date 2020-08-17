package org.virtuslab.inkuire.intellij.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.ui.components.JBList
import org.apache.http.client.utils.URIBuilder
import org.jdesktop.swingx.prompt.PromptSupport
import org.jetbrains.annotations.NotNull
import java.awt.Color
import java.awt.Dimension
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import javax.swing.*


class QueryAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val client = HttpClient.newHttpClient()

        val input = JTextField().apply {
            preferredSize = Dimension(500, 30)
            PromptSupport.setPrompt("List<String>.() -> Int", this)
        }

        val resultArea = JBList<String>().apply {
            border = BorderFactory.createLineBorder(Color.GRAY)
            preferredSize = Dimension(600, 300)
        }

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

            val array = parseStringArray(response.body())

            resultArea.setListData(array)
        }

        val searchButton = JButton("Search").apply {
            addActionListener { searchListener() }
        }


        val dialogPanel = JPanel().apply {
            preferredSize = Dimension(700, 350)
            add(searchButton)
            add(resultArea)
            add(input)
        }

        val db = DialogBuilder().apply {
            setOkOperation(searchListener)
            setCenterPanel(dialogPanel)
        }

        db.show()
    }

    private fun parseStringArray(str: String): Array<String> {
        val size = str.length
        return str.take(size - 1).takeLast(size - 2).split(", ").map {
            val s = it.length
            it.take(s - 1).takeLast(s - 2)
        }.toTypedArray()
    }
}