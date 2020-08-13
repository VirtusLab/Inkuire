package org.virtuslab.inkuire.intellij.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
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
    override fun actionPerformed(@NotNull e: AnActionEvent) {
        val client = HttpClient.newHttpClient()

        val db = DialogBuilder()

        val dialogPanel = JPanel()
        dialogPanel.preferredSize = Dimension(700, 350)

        val input = JTextField()
        input.preferredSize = Dimension(500, 30)
        PromptSupport.setPrompt("List<String>.() -> Int", input)
        dialogPanel.add(input)

        val resultArea = JList(emptyArray<String>())
        resultArea.border = BorderFactory.createLineBorder(Color.GRAY)
        resultArea.preferredSize = Dimension(600, 300)

        val searchButton = JButton("Search")

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
        searchButton.addActionListener { searchListener() }
        dialogPanel.add(searchButton)

        dialogPanel.add(resultArea)

        db.setOkOperation(searchListener)

        db.setCenterPanel(dialogPanel)
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