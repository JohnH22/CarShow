package gr.ihu.ict.carshow.ui.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// A reusable rating bar that displays stars
// If pass an "onRatingChanged" function the stars become clickable so the user can rate
// If not pass it the bar is read-only (just for showing the score)
@Composable
fun StarRatingBar(
    rating: Float,
    maxStars: Int = 5,
    onRatingChanged: ((Float) -> Unit)? = null // Nullable callback determines if the bar is interactive
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..maxStars) {
            // Check if this specific star should be filled or empty
            val isSelected = i <= rating
            // Choose filled or border icon based on selection state
            val icon = if (isSelected) Icons.Filled.Star else Icons.Filled.StarBorder
            val iconColor = if (isSelected) Color(0xFFFFB300) else Color.Gray


            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(24.dp)
                    .then(
                        // If a callback is provided, make the star clickable to update rating (only if want user to change the rating)
                        if (onRatingChanged != null) {
                            Modifier.clickable{ onRatingChanged(i.toFloat()) }
                        } else {
                            Modifier
                        }
                    )
            )
        }

        // Visual indicator of the numerical rating (e.g. , 4.5/5 )
        Text(
            text = "$rating/$maxStars",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}



// A single review card item used to show a user's comment, rating and date
@Composable
fun ReviewItem(
    username: String,
    rating: Float,
    comment: String,
    date: String,
    onDelete: (() -> Unit)? = null
) {

    // Extracts only the YYYY-MM-DD date part from the ISO timestamp by removing the 'T' separator and time from Django Server
    val cleanDate = if (date.contains("T")) date.substringBefore("T") else date

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        // Dark theme color
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display the name of the user who wrote the review
                Text(
                    text = username.ifBlank { "Anonymous" },
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    // Display the date the review was posted
                    Text(
                        text = cleanDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )


                    if (onDelete != null) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Review",
                            tint = Color(0xFFE53935),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable{ onDelete() }
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reuse StarRatingBar in read-only mode showing the user's score (onRatingChanges = null)
            StarRatingBar(rating = rating)

            Spacer(modifier = Modifier.height(8.dp))

            // Display the actual review text comment
            Text(
                text = comment,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
        }
    }
}


// A pop-up dialog box that lets the user select stars and type a comment to write as new review
@Composable
fun AddReviewDialog(
    onDismiss: () -> Unit,
    onConfirm: (Float, String) -> Unit
) {
    // Temporary Local state to hold user input (what user selects/types) before hitting submit button
    var rating by remember{ mutableStateOf(0.0f) }
    var comment by remember { mutableStateOf("") }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Write a Review",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "How would you rate this vehicle?"
                )

                // Reuse StarRatingBar in interactive mode so the user can click stars
                StarRatingBar(
                    rating = rating,
                    onRatingChanged = { rating = it }
                )

                // Text field where user types their text comment
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your Comments") },
                    placeholder = { Text("Describe your experience...") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    minLines = 3 // Gives enough vertical space for typing comment
                )
            }
        },
        confirmButton = {
            Button(
                // Trigger callback for sending the selected rating and text comment back when clicked
                onClick = { onConfirm(rating, comment) },
                // Validation, disable button if no rating is given or comment is empty
                enabled = rating > 0f && comment.isNotBlank()
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            // Standard cancel action to close the dialog (pop-up window) without saving
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}