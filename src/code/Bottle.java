package code;

import java.util.*;

public class Bottle {
    public Stack<Color> colors;
    public int capacity;

    public Bottle(int size, String[] initialColors) {
        this.capacity = size;
        this.colors = new Stack<>();
        for (int i = initialColors.length - 1; i >= 0; i--) {
            Color color = parseColor(initialColors[i]);
            if (color != Color.e) {
                colors.push(color);
            }
        }
    }

    private Color parseColor(String colorStr) {
        switch (colorStr) {
            case "r":
                return Color.red;
            case "g":
                return Color.green;
            case "b":
                return Color.blue;
            case "y":
                return Color.yellow;
            case "o":
                return Color.orange;
            default:
                return Color.e;
        }
    }

    public boolean isFull() {
        return colors.size() == capacity;
    }

    public boolean isEmpty() {
        return colors.isEmpty();
    }

    public Color topColor() {
        if (!colors.isEmpty()) {
            return colors.peek();
        }
        return Color.e;
    }

    public boolean canPourInto(Bottle target) {
        if (this.isEmpty() || target.isFull()) {
            return false;
        }
        Color targetTop = target.topColor();
        return targetTop == Color.e || targetTop == this.topColor();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Bottle other = (Bottle) obj;

        if (this.colors.size() != other.colors.size())
            return false;

        return Objects.equals(this.colors, other.colors);
    }

    public int pour(Bottle target) {
        int cost = 0;
        if (canPourInto(target)) {
            Color pouringColor = this.topColor();
            while (!this.isEmpty() && this.topColor() == pouringColor && !target.isFull()) {
                target.colors.push(this.colors.pop());
                cost++;
            }
        }
        return cost;
    }

}
