// server/wordlist.js

const wordPool = [
  "apple", "banana", "car", "house", "tree", "cloud", "sun", "moon", "river",
  "book", "laptop", "pen", "pencil", "chair", "sofa", "bottle", "phone",
  "guitar", "pizza", "lion", "elephant", "mountain", "ocean", "rain", "snow",
  "train", "bus", "rocket", "star", "bird", "fish", "egg", "ice", "drum",
  "camera", "glasses", "flag", "cake", "watch", "shoe", "key", "door",
  "fan", "wallet", "hat", "toothbrush", "broom", "ball", "kite", "mirror"
];

function getRandomWords(count = 3) {
  const shuffled = wordPool.sort(() => 0.5 - Math.random());
  return shuffled.slice(0, count);
}

module.exports = { getRandomWords };
